import os, glob
base = r'D:\111网安学习\menstrual-tracker\miniprogram'
count = 0
for root, dirs, files in os.walk(base):
    for f in files:
        if f.endswith(('.json', '.wxss', '.wxml', '.js')):
            fp = os.path.join(root, f)
            data = open(fp, 'rb').read()
            if data[:3] == b'\xef\xbb\xbf':
                open(fp, 'wb').write(data[3:])
                print('Fixed:', os.path.relpath(fp, base))
                count += 1
print('Total files fixed:', count)
