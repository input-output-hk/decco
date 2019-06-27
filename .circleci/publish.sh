#!/usr/bin/env bash

set -eux

GPG_TTY=$(tty)
export GPG_TTY

gpg --version

echo $gpgPrivateKey | base64 --decode > gpg_key

gpg --batch --passphrase "$gpgPassphrase" --import gpg_key

rm gpg_key

gpg --list-secret-keys

mill -i src.io.iohk.decco.publish \
--sonatypeCreds "$username:$password"  \
--gpgPassphrase "$gpgPassphrase" \
--gpgKeyName "$gpgKeyName" \
--release false


