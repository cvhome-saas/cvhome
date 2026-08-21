import * as React from "react"
import {cn} from "./lib/utils"

/**
 * Renders an already-formatted price pair. Prices arrive formatted from the API — never format here.
 * Uses <del>/<ins> so assistive tech reads "was / now" correctly.
 */
function Price({
                   className,
                   finalPrice,
                   originalPrice,
                   discounted,
                   size = "md",
                   ...props
               }: React.ComponentProps<"p"> & {
    finalPrice: string | undefined
    originalPrice?: string
    discounted?: boolean
    size?: "sm" | "md" | "lg"
}) {
    if (!finalPrice) return null
    const sizes = {sm: "text-sm", md: "text-base", lg: "text-2xl"}
    return (
        <p data-slot="price" className={cn("flex flex-wrap items-baseline gap-x-2", sizes[size], className)} {...props}>
            {discounted && originalPrice ? (
                <>
                    <ins data-slot="price-final" className="font-semibold text-foreground no-underline">{finalPrice}</ins>
                    <del data-slot="price-original" className="text-muted-foreground">{originalPrice}</del>
                </>
            ) : (
                <span data-slot="price-final" className="font-semibold text-foreground">{finalPrice}</span>
            )}
        </p>
    )
}

export {Price}
