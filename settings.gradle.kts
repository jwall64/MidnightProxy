rootProject.name = "midnightproxy"

include("api")
include("proxy")

include("plugin-test", "plugin-jwt", "plugin-whitelist", "plugin-lastserver", "plugin-sql", "plugin-messenger", "plugin-resourcepack-v2")

/* Legacy */
include("plugin-resourcepack-v1")
