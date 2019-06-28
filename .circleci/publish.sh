#!/usr/bin/env bash

#set -eux

export GNUPGHOME=/root/.gnupg

GPG_TTY=$(tty)
export GPG_TTY

gpg --version

gpg-agent --version


echo $GPG_KEY | base64 --decode -i | gpg --batch --import --no-tty --yes --passphrase "$GPG_PASSPHRASE"


gpg --list-secret-keys

mill -i src.io.iohk.decco.publish \
--sonatypeCreds "$OSS_USERNAME:$OSS_PASSWORD"  \
--gpgPassphrase "$GPG_PASSPHRASE" \
--gpgKeyName "$GPG_KEY_ID" \
--release false

cd $GNUPGHOME
ls -lart

lastlog