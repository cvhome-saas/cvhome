import {decode} from 'html-entities';
import {Description} from "@store-front/types";

export function parseDescription(description: Description | undefined): string {
    if (description && description.description) {
        try {
            return decode(description.description);
        } catch (error) {
        }
    }
    return '';
}