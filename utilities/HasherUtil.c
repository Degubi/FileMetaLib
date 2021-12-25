#include "../MediaProps.c"

int main() {
    PROPERTYKEY* property = &PKEY_Author;

    return hashPropKey(property);
}