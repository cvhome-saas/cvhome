import React from "react";
import {cva, type VariantProps} from "class-variance-authority";
import {cn} from "@/lib/utils";

const sectionTitleVariants = cva("w-full", {
    variants: {
        align: {
            center: "text-center",
            left: "text-left",
            right: "text-right",
        },
    },
    defaultVariants: {
        align: "center",
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
                className={cn(sectionTitleVariants({align}), "relative pb-4 mb-8", className)}
                {...props}
            >
                <h2 className="text-3xl font-serif font-bold tracking-tight text-foreground sm:text-4xl">
                    {title}
                </h2>
                {subtitle && <p className="mt-3 text-lg text-muted-foreground font-light italic">{subtitle}</p>}
                <div className={cn(
                    "absolute bottom-0 h-1 w-20 bg-primary/30 rounded-full",
                    align === 'center' ? "left-1/2 -translate-x-1/2" : align === 'right' ? "right-0" : "left-0"
                )}/>
            </div>
        );
    }
);
SectionTitle.displayName = "SectionTitle";