import {decode} from 'html-entities';
import {Description} from "@/types/description";

export function parseDescription(description: Description | undefined): any {
    if (description && description.description) {
        try {
            return decode(description.description);
        } catch (error) {
        }
    }
    return '';
}