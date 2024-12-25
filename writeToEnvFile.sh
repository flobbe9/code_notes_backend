# NOTE: exclude characters: &\;'
# NOTE: in VSCode select line-endings "LF" in the bottom right

# place the .env and this .sh file inside same directory
ENV_FILE_NAME=$(dirname "$0")/.env

# case: file does not exist
if [ ! -f $ENV_FILE_NAME ]; then 
    touch $ENV_FILE_NAME;
fi

# arg expected to be formatted like VARIABLE=someValue
for arg in "$@"; do
    # include = char
    VARIABLE_NAME=$(echo $arg | grep -o '.*=');

    # get value with = char
    VARIABLE_VALUE=$(echo $arg | grep -o '=.*');

    # case: value does not start with a '
    if ! [[ $VARIABLE_VALUE == \=\'* ]]; then
        VARIABLE_VALUE=\'$(echo $VARIABLE_VALUE | cut -c2-)\';
    else
        VARIABLE_VALUE=$(echo $VARIABLE_VALUE | cut -c2-);
    fi

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