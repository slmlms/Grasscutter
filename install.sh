#!/usr/bin/env bash

# Grasscutter install script for linux
# Made by TurtleIdiot

# Stops the installer if any command has a non-zero exit status
set -e

# Checks for root
if [ $EUID != 0 ]; then
        echo "Please run the installer as root!"
        exit
fi

is_command() {
        # Checks if a given command is available
        local check_command="$1"
        command -v "${check_command}" > /dev/null 2>&1
}

# IP validation
valid_ip() {
        local ip=$1
        local stat=1

        if [[ $ip =~ ^[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}$ ]]; then
                OIFS=$IFS
                IFS="."
                ip=($ip)
                IFS=$OIFS
                [[ ${ip[0]} -le 255 && ${ip[1]} -le 255 \
                        && ${ip[2]} -le 255 && ${ip[3]} -le 255 ]]
                stat=$?
        fi
        return $stat
}

# Checks for supported installer(s) (only apt-get for now :(, might add pacman in the future)
if is_command apt-get ; then
        echo -e "Supported package manager found\n"
else
        echo "No supported package manager found"
        exit
fi

INSTALLER_DEPS="curl wget openssl unzip git"
GC_DEPS="mongodb openjdk-17-jre"

echo "The following packages will have to be installed in order to INSTALL grasscutter:"
echo -e "$INSTALLER_DEPS \n"
echo "The following packages will have to be installed to RUN grasscutter:"
echo -e "$GC_DEPS \n"

echo "Do you wish to proceed and install grasscutter?"
select yn in "Yes" "No" ; do
        case $yn in
                Yes ) break;;
                No ) exit;;
        esac
done

echo "Updating package cache..."
apt-get update -qq > /dev/null

# Starts installing dependencies
echo "Installing setup dependencies..."
apt-get -qq install $INSTALLER_DEPS -y > /dev/null
echo "Done"

echo "Installing grasscutter dependencies..."
apt-get -qq install $GC_DEPS -y > /dev/null
echo "Done"

echo "Getting grasscutter..."

# Download and rename jar
wget -q --show-progress https://nightly.link/Grasscutters/Grasscutter/workflows/build/stable/Grasscutter.zip
unzip -qq Grasscutter.zip
mv $(find -name "grasscutter*.jar" -type f) grasscutter.jar

# Download resources
echo "Downloading resources... (this will take a moment)"
wget -q --show-progress https://github.com/Koko-boya/Grasscutter_Resources/archive/refs/heads/main.zip -O resources.zip
echo "Extracting..."
unzip -qq resources.zip
mv ./Grasscutter_Resources-main/Resources ./resources

# Here we do a sparse checkout to only pull /data and /keys
echo "Downloading keys and data..."
mkdir repo
cd repo
git init -q
git remote add origin https://github.com/Grasscutters/Grasscutter.git
git fetch -q
git config core.sparseCheckout true
echo "data/" >> .git/info/sparse-checkout
echo "keys/" >> .git/info/sparse-checkout
git pull origin stable -q
cd ../
mv ./repo/data ./data
mv ./repo/keys ./keys

# Generate handbook/config
echo "Please enter language when *NEXT* prompted (press enter/return to continue to language select)"
read
java -jar grasscutter.jar -handbook

# Prompt IP address for config.json and for generating new keystore.p12 file
echo "Please enter the IP address that will be used to connect to the server"
echo "This can be a local or a public IP address"
echo "This IP address will be used to generate SSL certificates so it is important it is correct"

while : ; do
        read -p "Enter IP: " SERVER_IP
        if valid_ip $SERVER_IP; then
                break;
        else
                echo "Invalid IP address. Try again."
        fi
done

# Replaces "127.0.0.1" with given IP
sed -i "s/127.0.0.1/$SERVER_IP/g" config.json

# Generates new keystore.p12 with the server's IP address
# This is done to prevent a "Connection Timed Out" error from appearing
#       after clicking to enter the door in the main menu/title screen
# This issue only exists when connecting to a server *other* than localhost
#       since the default keystore.p12 has only been made for localhost

mkdir certs
cd certs
echo "Generating CA key and certificate pair..."
openssl req -x509 -nodes -days 25202 -newkey rsa:2048 -subj "/C=GB/ST=Essex/L=London/O=Grasscutters/OU=Grasscutters/CN=$SERVER_IP" -keyout CAkey.key -out CAcert.crt
echo "Generating SSL key and certificate pair..."

openssl genpkey -out ssl.key -algorithm rsa

# Creates a conf file in order to generate a csr
cat > csr.conf <<EOF
[ req ]
default_bits = 2048
prompt = no
default_md = sha256
req_extensions = req_ext
distinguished_name = dn

[ dn ]
C = GB
ST = Essex
L = London
O = Grasscutters
OU = Grasscutters
CN = $SERVER_IP

[ req_ext ]
subjectAltName = @alt_names

[ alt_names ]
IP.1 = $SERVER_IP
EOF

# Creates csr using key and conf
openssl req -new -key ssl.key -out ssl.csr -config csr.conf

# Creates conf to finalise creation of certificate
cat > cert.conf <<EOF

authorityKeyIdentifier=keyid,issuer
basicConstraints=CA:FALSE
keyUsage = digitalSignature, nonRepudiation, keyEncipherment, keyAgreement, dataEncipherment
subjectAltName = @alt_names

[alt_names]
IP.1 = $SERVER_IP

EOF

# Creates ssl cert
openssl x509 -req -in ssl.csr -CA CAcert.crt -CAkey CAkey.key -CAcreateserial -out ssl.crt -days 25202 -sha256 -extfile cert.conf

echo "Generating keystore.p12 from key and certificate..."
openssl pkcs12 -export -out keystore.p12 -inkey ssl.key -in ssl.crt -certfile CAcert.crt -passout pass:123456

cd ../
mv ./certs/keystore.p12 ./keystore.p12
echo "Done"

echo -e "Asking Noelle to clean up...\n"
rm -rf Grasscutter.zip resources.zip ./certs ./Grasscutter_Resources-main ./repo
echo -e "All done!\n"
echo -e "You can now uninstall the following packages if you wish:\n$INSTALLER_DEPS"
echo -e "-=-=-=-=-=--- !! IMPORTANT !! ---=-=-=-=-=-\n"
echo "Please make sure that ports 443 and 22102 are OPEN (both tcp and udp)"
echo -e "In order to run the server, run the following command:\nsudo java -jar grasscutter.jar"
echo "You must run it using sudo as port 443 is a privileged port"
echo "To play, use the IP you provided earlier ($SERVER_IP) via GrassClipper or Fiddler"

exit
