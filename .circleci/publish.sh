#!/usr/bin/env bash

set -eux

GPG_TTY=$(tty)
export GPG_TTY

gpg --version

echo $GPG_KEY | base64 --decode > gpg_key

gpg --batch --passphrase "$GPG_PASSPHRASE" --import --no-tty --yes gpg_key

rm gpg_key

gpg --list-secret-keys

mill -i src.io.iohk.decco.publish \
--sonatypeCreds "$OSS_USERNAME:$OSS_PASSWORD"  \
--gpgPassphrase "$GPG_PASSPHRASE" \
--gpgKeyName "$GPG_KEY_ID" \
--release false


