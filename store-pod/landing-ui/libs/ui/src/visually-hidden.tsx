import * as React from "react"

/** Screen-reader-only text. Prefer this over ad-hoc `sr-only` spans so the intent is greppable. */
function VisuallyHidden({children, ...props}: React.ComponentProps<"span">) {
    return <span className="sr-only" {...props}>{children}</span>
}

export {VisuallyHidden}
