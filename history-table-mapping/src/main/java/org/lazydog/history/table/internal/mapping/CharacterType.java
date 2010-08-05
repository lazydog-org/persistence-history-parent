package org.lazydog.history.table.internal.mapping;


/**
 * Character type.
 *
 * @author  Ron Rickard
 */
public enum CharacterType {
    DIGIT,
    LOWERCASE,
    OTHER,
    UPPERCASE;

    /**
     * Get the character type for the specified character.
     *
     * @param  character  the character to determine the character type for.
     *
     * @return  the character type.
     */
    public static CharacterType get(char character) {

        // Declare.
        CharacterType characterType;

        // Set the character type to OTHER.
        characterType = OTHER;

        // Check if the character is a digit.
        if (Character.isDigit(character)) {

            // Set the character type to DIGIT.
            characterType = DIGIT;
        }

        // Check if the character is lowercase.
        else if (Character.isLowerCase(character)) {

            // Set the character type to LOWERCASE.
            characterType = LOWERCASE;
        }

        // Check if the character is upercase.
        else if (Character.isUpperCase(character)) {

            // Set the character type to UPPERCASE.
            characterType = UPPERCASE;
        }
        
        return characterType;
    }
}
