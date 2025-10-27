export const getDirection = ((locale: string) => {
    switch (locale) {
        case "ar": {
            return "rtl";
        }
        default: {
            return "ltr";
        }
    }

});
export const isRtl = (locale: string) => {
    return getDirection(locale) === "rtl";
};
export const isLtr = (locale: string) => {
    return getDirection(locale) === "ltr";
};
