package com.menstrualtracker.user.service;

import com.menstrualtracker.common.cache.CacheService;
import com.menstrualtracker.common.cache.LoginRateLimiter;
import com.menstrualtracker.common.cache.TokenBlacklistCache;
import com.menstrualtracker.common.dto.ApiResponse;
import com.menstrualtracker.common.exception.BusinessException;
import com.menstrualtracker.common.util.JwtUtil;
import com.menstrualtracker.user.dto.*;
import com.menstrualtracker.user.entity.User;
import com.menstrualtracker.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final CacheService cacheService;
    private final LoginRateLimiter loginRateLimiter;
    private final TokenBlacklistCache tokenBlacklistCache;

    @Value("${wechat.app-id}")
    private String wechatAppId;

    @Value("${wechat.app-secret}")
    private String wechatAppSecret;

    // Token 濞村吋淇洪惁鐣岀磽閹惧磭鎽犻柨?濠㈠灈鏅槐娆愮▔?JWT 閺夆晛娲﹀﹢锟犲籍閸洘锛熷☉鎾亾闁肩柉鎻槐?
    private static final String TOKEN_CACHE_KEY = "menstrual:token:%s";
    private static final long TOKEN_CACHE_TTL = 7; // 濠?

    // 鐎甸偊鍠曟穱?access_token 缂傚倹鎸搁悺銊╂晬?000缂?
    private static final String WECHAT_TOKEN_KEY = "menstrual:wechat:access_token";
    private static final long WECHAT_TOKEN_TTL = 7000; // 缂?

    @Transactional
    public ApiResponse<LoginResponse> register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername()))
            throw BusinessException.conflict("Username already exists");
        if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail()))
            throw BusinessException.conflict("Email already exists");
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone()).email(request.getEmail()).build();
        user = userRepository.save(user);
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        // 閻忓繐妫旂槐鎵嫚濠靛洦衼閻忓繐瀚悺銊╁礂?Redis闁?濠㈠灈鏅炵换鍐嫉閻曞倻绀?
        cacheService.set(String.format(TOKEN_CACHE_KEY, token), user.getId(), TOKEN_CACHE_TTL, TimeUnit.DAYS);
        return ApiResponse.success("Registration successful", buildLoginResponse(user, token));
    }

    public ApiResponse<LoginResponse> login(LoginRequest request, String clientIp) {
        // 閺堫剙婀撮梽鎰ウ閿涘牅绗夋笟婵婄 Redis閿?
        if (!loginRateLimiter.isAllowed(clientIp)) {
            throw BusinessException.tooManyRequests("Too many login attempts, please try again later");
        }
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> BusinessException.unauthorized("Invalid username or password"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword()))
            throw BusinessException.unauthorized("Invalid username or password");
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        // 閻忓繐妫旂槐鎵嫚濠靛洦衼閻忓繐瀚悺銊╁礂?Redis闁?濠㈠灈鏅炵换鍐嫉閻曞倻绀?
        cacheService.set(String.format(TOKEN_CACHE_KEY, token), user.getId(), TOKEN_CACHE_TTL, TimeUnit.DAYS);
        loginRateLimiter.onSuccess(clientIp);
        return ApiResponse.success("Login successful", buildLoginResponse(user, token));
    }

    /**
     * 闁活潿鍔嶉崺娑樷枖閵娾晜鏁?闁?闁告帞濞€濞?Redis 濞戞搩鍘惧▓?Token 濞村吋淇洪惁浠嬪及閻樿尙娈搁柕?
     */
    public ApiResponse<Void> logout(String token) {
        if (token != null && !token.isEmpty()) {
            // 1. 尝试从 Redis 删除
            cacheService.delete(String.format(TOKEN_CACHE_KEY, token));
            // 2. 本地黑名单兜底（Redis 宕机时仍生效）
            tokenBlacklistCache.add(token);
            log.info("User logged out, token session removed from Redis + local blacklist");
        }
        return ApiResponse.success("Logged out", null);
    }

    public ApiResponse<LoginResponse> wxLogin(String code, String clientIp) {
        // 閺堫剙婀撮梽鎰ウ閿涘牅绗夋笟婵婄 Redis閿?
        if (!loginRateLimiter.isAllowed(clientIp)) {
            throw BusinessException.tooManyRequests("Too many login attempts, please try again later");
        }
        String openId;
        String mockUsername = "wx_user";

        if (wechatAppId.startsWith("test_")) {
            openId = mockOpenId(code);
            mockUsername = "test_user_" + openId.substring(0, 8);
        } else {
            openId = callWeChatApi(code);
            if (openId == null) {
                throw BusinessException.badRequest("WeChat login failed");
            }
        }

        User user = userRepository.findByOpenId(openId).orElse(null);

        if (user == null) {
            user = User.builder()
                    .username(mockUsername)
                    .openId(openId)
                    .password(passwordEncoder.encode(openId))
                    .build();
            user = userRepository.save(user);
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        // 閻忓繐妫旂槐鎵嫚濠靛洦衼閻忓繐瀚悺銊╁礂?Redis闁?濠㈠灈鏅炵换鍐嫉閻曞倻绀?
        cacheService.set(String.format(TOKEN_CACHE_KEY, token), user.getId(), TOKEN_CACHE_TTL, TimeUnit.DAYS);
        loginRateLimiter.onSuccess(clientIp);
        return ApiResponse.success("Login successful", buildLoginResponse(user, token));
    }

    public ApiResponse<UserProfileDTO> getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> BusinessException.notFound("User not found"));
        return ApiResponse.success(toProfileDTO(user));
    }

    @Transactional
    public ApiResponse<UserProfileDTO> updateProfile(Long userId, UserProfileDTO request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> BusinessException.notFound("User not found"));
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        if (request.getEmail() != null) {
            if (!request.getEmail().equals(user.getEmail()) && userRepository.existsByEmail(request.getEmail()))
                throw BusinessException.conflict("Email already exists");
            user.setEmail(request.getEmail());
        }
        if (request.getAvatar() != null) user.setAvatar(request.getAvatar());
        if (request.getBirthDate() != null) user.setBirthDate(request.getBirthDate());
        if (request.getMenarcheAge() != null) user.setMenarcheAge(request.getMenarcheAge());
        if (request.getAvgCycleDays() != null) user.setAvgCycleDays(request.getAvgCycleDays());
        if (request.getAvgPeriodDays() != null) user.setAvgPeriodDays(request.getAvgPeriodDays());
        user = userRepository.save(user);
        return ApiResponse.success("Profile updated", toProfileDTO(user));
    }

    private String mockOpenId(String code) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest((wechatAppId + ":" + code).getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) hex.append(String.format("%02x", b));
            return "mock_" + hex.substring(0, 24);
        } catch (Exception e) {
            return "mock_" + System.currentTimeMillis();
        }
    }

    private String callWeChatApi(String code) {
        try {
            String urlStr = "https://api.weixin.qq.com/sns/jscode2session"
                    + "?appid=" + wechatAppId
                    + "&secret=" + wechatAppSecret
                    + "&js_code=" + code
                    + "&grant_type=authorization_code";
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder resp = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) resp.append(line);
            reader.close();
            String json = resp.toString();
            if (json.contains("\"openid\"")) {
                int start = json.indexOf("\"openid\"") + 9;
                start = json.indexOf("\"", start) + 1;
                int end = json.indexOf("\"", start);
                return json.substring(start, end);
            }
            log.error("WeChat API error: {}", json);
            return null;
        } catch (Exception e) {
            log.error("WeChat API call failed", e);
            return null;
        }
    }

    /**
     * 闁兼儳鍢茶ぐ鍥ь嚗椤旇绻?access_token 闁?濞村吋锚閸樻稒绂?Redis 閻犲洩顕цぐ鍥晬鐏炵偓寮撻柛娑欏灊閼垫垿宕氬▎鎺旀闁活潿鍔屾禍鏇熺┍閳╁啫澶嶉柛娆欑到閼荤喓绱撻幘宕囨憼闁?000缂佸甯槐姘跺Υ?
     * 闁活潿鍔嬬花顒勫触鎼达絿鏁鹃悹瀣暟閺併倕顕ラ璁崇箚闁稿繑婀圭划顒勫嫉瀹ュ懎顫ょ紒鏃戝灡鐢挳宕ｉ敐蹇曠濠碘€冲€歌ぐ鍌炴焻娴ｉ紦渚€寮堕幐搴Ｐラ柟顓у灲缁辨岸濡?
     */
    public String getWechatAccessToken() {
        // 1. 闁?Redis 缂傚倹鎸搁悺?
        Object cached = cacheService.get(WECHAT_TOKEN_KEY);
        if (cached instanceof String && !((String) cached).isEmpty()) {
            log.debug("WeChat access_token hit from Redis cache");
            return (String) cached;
        }

        // 2. 缂傚倹鎸搁悺銊╁嫉椤忓嫭鍤掑☉鎿冨弿缁辨繄鎷崘顏呮殢鐎甸偊鍠曟穱濠囧箳閵夈儱缍撻柤鎯у槻瑜?
        try {
            String urlStr = "https://api.weixin.qq.com/cgi-bin/token"
                    + "?grant_type=client_credential"
                    + "&appid=" + wechatAppId
                    + "&secret=" + wechatAppSecret;
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder resp = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) resp.append(line);
            reader.close();
            String json = resp.toString();
            if (json.contains("\"access_token\"")) {
                int start = json.indexOf("\"access_token\"") + 16;
                start = json.indexOf("\"", start) + 1;
                int end = json.indexOf("\"", start);
                String accessToken = json.substring(start, end);
                // 3. 闁告劖鐟ラ崣?Redis 缂傚倹鎸搁悺銊╂晬?000缂佸甯槐婵囩▔鎼粹€茬俺濞ｅ毝銈囩闁搞儳鍋熷▓?expires_in 闁规亽鍎寸换搴ㄦ晬?
                cacheService.set(WECHAT_TOKEN_KEY, accessToken, WECHAT_TOKEN_TTL, TimeUnit.SECONDS);
                log.info("WeChat access_token retrieved and cached");
                return accessToken;
            }
            log.error("Failed to get WeChat access_token: {}", json);
        } catch (Exception e) {
            log.error("WeChat access_token API call failed", e);
        }
        return null;
    }

    private LoginResponse buildLoginResponse(User user, String token) {
        return LoginResponse.builder().token(token).tokenType("Bearer").expiresIn(86400000L).user(toProfileDTO(user)).build();
    }

    private UserProfileDTO toProfileDTO(User user) {
        return UserProfileDTO.builder()
                .id(user.getId()).username(user.getUsername()).phone(user.getPhone())
                .email(user.getEmail()).avatar(user.getAvatar()).birthDate(user.getBirthDate())
                .menarcheAge(user.getMenarcheAge()).avgCycleDays(user.getAvgCycleDays())
                .avgPeriodDays(user.getAvgPeriodDays()).build();
    }
}