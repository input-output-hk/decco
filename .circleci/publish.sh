#!/usr/bin/env bash

set -eu

if [[ "$CIRCLE_BRANCH" == "develop" ]]; then

echo $GPG_KEY | base64 --decode | gpg --batch --import --passphrase "$GPG_PASSPHRASE"

gpg --passphrase $GPG_PASSPHRASE --batch --yes -a -b LICENSE

mill src.io.iohk.decco.publish \
--sonatypeCreds "$OSS_USERNAME:$OSS_PASSWORD"  \
--gpgPassphrase "$GPG_PASSPHRASE" \
--gpgKeyName "$GPG_KEY_ID" \
--release false

mill src.io.iohk.decco.auto.publish \
--sonatypeCreds "$OSS_USERNAME:$OSS_PASSWORD"  \
--gpgPassphrase "$GPG_PASSPHRASE" \
--gpgKeyName "$GPG_KEY_ID" \
--release false

else

  echo "Skipping publish step"

fi