#!/usr/bin/env bash

set -eux

echo $gpgPrivateKey | base64 --decode > gpg_key

gpg --import gpg_key

rm gpg_key

mill src.io.iohk.decco.publish \
--sonatypeCreds "$username:$password"  \
--gpgPassphrase "$gpgPassphrase" \
--release false
