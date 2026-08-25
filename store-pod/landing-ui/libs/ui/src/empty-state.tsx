import * as React from "react"
import {cn} from "./lib/utils"

/**
 * Headless-ish empty state: icon slot, title, body, action. Themes restyle through tokens and className,
 * or compose their own wrapper — they should not re-invent the markup (a11y: region + heading).
 */
function EmptyState({
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
            data-slot="empty-state"
            role="status"
            className={cn("flex flex-col items-center justify-center gap-3 py-12 text-center", className)}
            {...props}
        >
            {icon && <div data-slot="empty-state-icon" className="text-muted-foreground [&_svg]:size-10">{icon}</div>}
            <h2 data-slot="empty-state-title" className="text-base font-medium text-foreground">{title}</h2>
            {children && <div data-slot="empty-state-body" className="max-w-prose text-sm text-muted-foreground">{children}</div>}
            {action && <div data-slot="empty-state-action" className="mt-2 flex flex-wrap justify-center gap-2">{action}</div>}
        </section>
    )
}

export {EmptyState}
