"use client"

import * as React from "react"
import {useDir} from "@store-front/i18n/use-dir"
import {Sheet, SheetClose, SheetContent, SheetDescription, SheetFooter, SheetHeader, SheetTitle, SheetTrigger} from "./sheet"

type Side = "start" | "end" | "top" | "bottom"

/**
 * Sheet whose side is expressed in logical terms (`start` / `end`) and resolved against the reading
 * direction — so a cart drawer opens from the reading-end side in Arabic without every theme branching.
 */
function DrawerContent({side = "end", ...props}: Omit<React.ComponentProps<typeof SheetContent>, "side"> & { side?: Side }) {
    const dir = useDir()
    const physical =
        side === "start" ? (dir === "rtl" ? "right" : "left")
            : side === "end" ? (dir === "rtl" ? "left" : "right")
                : side
    return <SheetContent side={physical} {...props} />
}

export {
    Sheet as Drawer,
    SheetTrigger as DrawerTrigger,
    SheetClose as DrawerClose,
    DrawerContent,
    SheetHeader as DrawerHeader,
    SheetFooter as DrawerFooter,
    SheetTitle as DrawerTitle,
    SheetDescription as DrawerDescription,
}
