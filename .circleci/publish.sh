#!/usr/bin/env bash

set -eu

echo $GPG_KEY | base64 --decode -i | gpg --batch --import --no-tty --yes --passphrase "$GPG_PASSPHRASE"

gpg --passphrase $GPG_PASSPHRASE --batch --yes -a -b LICENSE

mill -i src.io.iohk.decco.publish \
--sonatypeCreds "$OSS_USERNAME:$OSS_PASSWORD"  \
--gpgPassphrase "$GPG_PASSPHRASE" \
--gpgKeyName "$GPG_KEY_ID" \
--release false
