FROM nats:2.12.7

COPY --from=busybox:1.36.1-musl /bin/busybox /bin/busybox
