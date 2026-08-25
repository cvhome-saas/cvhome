import {showToast} from "nextjs-toast-notify";
import {toastDirection} from "@store-front/services/direction-utils";

/** One toast vocabulary for every hook and theme. */
export function notify(kind: 'success' | 'error' | 'info', message: string, locale: string) {
    const options = {
        duration: 3000,
        progress: false,
        position: toastDirection(locale),
        transition: "bounceIn",
        sound: false,
    } as const;
    if (kind === 'success') showToast.success(message, options);
    else if (kind === 'error') showToast.error(message, options);
    else showToast.info(message, options);
}
