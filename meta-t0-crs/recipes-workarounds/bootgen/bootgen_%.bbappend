# bootgen 2025.2 has a missing #include <stdio.h> in lms-hash-sigs/hss_param.c,
# which fails with GCC 14+ (-Wimplicit-function-declaration is now an error).
CFLAGS:append = " -Wno-error=implicit-function-declaration"
