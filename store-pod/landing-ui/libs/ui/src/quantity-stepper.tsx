"use client"

import * as React from "react"
import {MinusIcon, PlusIcon} from "lucide-react"
import {Button} from "./button"
import {cn} from "./lib/utils"

/**
 * Accessible − / value / + control. Behaviour lives in the caller (hooks), this only renders and wires.
 * `decrementLabel` / `incrementLabel` are required so no theme ships an unlabeled icon button.
 */
function QuantityStepper({
                             className,
                             value,
                             onDecrement,
                             onIncrement,
                             canDecrement = true,
                             canIncrement = true,
                             decrementLabel,
                             incrementLabel,
                             size = "default",
                             ...props
                         }: Omit<React.ComponentProps<"div">, "onChange"> & {
    value: number
    onDecrement: () => void
    onIncrement: () => void
    canDecrement?: boolean
    canIncrement?: boolean
    decrementLabel: string
    incrementLabel: string
    size?: "sm" | "default"
}) {
    return (
        <div
            data-slot="quantity-stepper"
            role="group"
            className={cn("inline-flex items-center gap-1", className)}
            {...props}
        >
            <Button type="button" variant="outline" size={size === "sm" ? "icon-sm" : "icon"}
                    onClick={onDecrement} disabled={!canDecrement} aria-label={decrementLabel}>
                <MinusIcon/>
            </Button>
            <output data-slot="quantity-value" aria-live="polite"
                    className={cn("min-w-8 text-center font-medium tabular-nums", size === "sm" ? "text-sm" : "text-base")}>
                {value}
            </output>
            <Button type="button" variant="outline" size={size === "sm" ? "icon-sm" : "icon"}
                    onClick={onIncrement} disabled={!canIncrement} aria-label={incrementLabel}>
                <PlusIcon/>
            </Button>
        </div>
    )
}

export {QuantityStepper}
