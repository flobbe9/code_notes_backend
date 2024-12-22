# place the .env and this .sh file inside same directory
ENV_FILE_NAME=$(dirname "$0")/.env

# case: file does not exist
if [ ! -f $ENV_FILE_NAME ]; then 
    touch $ENV_FILE_NAME;
fi

# arg expected to be formatted like VARIABLE=someValue
for arg in "$@"; do
    VARIABLE_NAME=$(echo $arg | grep -o '.*=');

    # get value without '=' and with single quotes
    VARIABLE_VALUE=$(echo $arg | grep -o '=.*');
    VARIABLE_VALUE=\'$(echo $VARIABLE_VALUE | cut -c2-)\';

    # concat
    VARIABLE="$VARIABLE_NAME$VARIABLE_VALUE";

    # case: VARIABLE_NAME not present in file
    if ! grep -xq "^"$VARIABLE_NAME".*$" $ENV_FILE_NAME; then 
        # add var on new line
        printf "\n"$VARIABLE >> $ENV_FILE_NAME; 

    # case: VARIABLE_NAME present in file
    else 
        # replace var
        sed -i -e "s/^"$VARIABLE_NAME".*$/"$VARIABLE"/" $ENV_FILE_NAME; 
    fi
done