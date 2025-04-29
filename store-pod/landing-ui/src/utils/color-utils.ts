import {ColorSchema} from "@/types/color-schema";

function toKebabCase(input: string): string {
    return input.replace(/([a-z])([A-Z])/g, '$1-$2').toLowerCase();
}

export function toRootStyle(colors: ColorSchema) {
    return Object.entries(colors)
        .map(([key, value]) => `--${toKebabCase(key)}: ${value};`)
        .join("\n");
}