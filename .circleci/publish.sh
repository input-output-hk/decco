#!/usr/bin/env bash

set -eux

gpg --version

echo $gpgPrivateKey | base64 --decode > gpg_key

gpg --batch --passphrase "$gpgPassphrase" --import gpg_key

rm gpg_key

gpg --list-secret-keys

mill src.io.iohk.decco.publish \
--sonatypeCreds "$username:$password"  \
--gpgPassphrase "$gpgPassphrase" \
--gpgKeyName "$gpgKeyName" \
--release false


