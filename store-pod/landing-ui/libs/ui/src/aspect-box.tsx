import * as React from "react"
import {cn} from "./lib/utils"

/**
 * Fixed-ratio box for product imagery. Defaults to the theme's `--aspect-product` token so every image
 * surface in a theme agrees; pass `ratio` to override locally (e.g. a hero).
 */
function AspectBox({className, ratio, style, ...props}: React.ComponentProps<"div"> & { ratio?: string }) {
    return (
        <div
            data-slot="aspect-box"
            className={cn("relative w-full overflow-hidden", className)}
            style={{aspectRatio: ratio ?? "var(--aspect-product, 1 / 1)", ...style}}
            {...props}
        />
    )
}

export {AspectBox}
