FROM ubuntu:22.04

ENV DEBIAN_FRONTEND noninteractive

RUN apt-get update -qq \
	&& apt-get install -y --no-install-recommends \
		build-essential gcc g++ \
		gawk wget git git-lfs diffstat unzip texinfo \
		chrpath socat cpio \
		python3 python3-pip python3-pexpect python3-git \
		python3-jinja2 python3-subunit python3-websockets \
		python3-yaml \
		xz-utils debianutils iputils-ping \
		zstd liblz4-tool file locales \
		libacl1 mesa-common-dev \
		ca-certificates \
		lz4 \
	&& rm -rf /var/lib/apt/lists/* /var/cache/apt/*

# Install libtinfo5, which Vivado still relies on
RUN wget -q http://security.ubuntu.com/ubuntu/pool/universe/n/ncurses/libtinfo5_6.3-2ubuntu0.1_amd64.deb \
	&& apt-get install -y ./libtinfo5_6.3-2ubuntu0.1_amd64.deb \
	&& rm libtinfo5_6.3-2ubuntu0.1_amd64.deb

# Yocto requires en_US.UTF-8
RUN sed -i '/en_US.UTF-8/s/^# //g' /etc/locale.gen && locale-gen
ENV LANG=en_US.UTF-8

# make /bin/sh symlink to bash instead of dash (required by Yocto)
RUN dpkg-divert --remove --no-rename /bin/sh \
	&& ln -sf bash /bin/sh \
	&& dpkg-divert --add --local --no-rename /bin/sh
