import React from "react";
import {cva, type VariantProps} from "class-variance-authority";
import {cn} from "@/lib/utils";

const sectionTitleVariants = cva("w-full", {
    variants: {
        align: {
            center: "text-center",
            left: "text-start",
            right: "text-end",
        },
    },
    defaultVariants: {
        align: "left",
    },
});

export interface SectionTitleProps
    extends React.HTMLAttributes<HTMLDivElement>,
        VariantProps<typeof sectionTitleVariants> {
    title: string;
    subtitle?: string;
}

export const SectionTitle = React.forwardRef<HTMLDivElement, SectionTitleProps>(
    ({className, title, subtitle, align, ...props}, ref) => {
        return (
            <div
                ref={ref}
                className={cn(sectionTitleVariants({align}), className)}
                {...props}
            >
                <h2 className="mt-2 text-2xl font-semibold tracking-tight text-foreground sm:text-3xl">
                    {title}
                </h2>
                {subtitle && <p className="mt-2 text-sm text-muted-foreground">{subtitle}</p>}
            </div>
        );
    }
);
SectionTitle.displayName = "SectionTitle";