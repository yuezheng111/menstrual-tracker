Component({ properties: { text: { type: String, value: "" }, progress: { type: Number, value: 0 } }, computed: { offset() { return 314 - (314 * this.properties.progress / 100) } } })
