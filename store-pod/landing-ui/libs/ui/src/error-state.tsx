import * as React from "react"
import {cn} from "./lib/utils"

/** Error block with optional retry/home actions. Pair with `ApiError` helpers to pick the message. */
function ErrorState({
                        className,
                        icon,
                        title,
                        children,
                        action,
                        ...props
                    }: React.ComponentProps<"section"> & {
    icon?: React.ReactNode
    title: React.ReactNode
    action?: React.ReactNode
}) {
    return (
        <section
            data-slot="error-state"
            role="alert"
            aria-live="polite"
            className={cn("flex flex-col items-center justify-center gap-3 py-12 text-center", className)}
            {...props}
        >
            {icon && <div data-slot="error-state-icon" className="text-destructive [&_svg]:size-10">{icon}</div>}
            <h2 data-slot="error-state-title" className="text-base font-medium text-foreground">{title}</h2>
            {children && <div data-slot="error-state-body" className="max-w-prose text-sm text-muted-foreground">{children}</div>}
            {action && <div data-slot="error-state-action" className="mt-2 flex flex-wrap justify-center gap-2">{action}</div>}
        </section>
    )
}

export {ErrorState}
