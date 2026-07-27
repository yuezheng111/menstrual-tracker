Component({ properties: { customClass: { type: String, value: "" }, customStyle: { type: String, value: "" } }, methods: { onTap() { this.triggerEvent("tap") } } })
