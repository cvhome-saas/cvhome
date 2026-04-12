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
                className={cn(sectionTitleVariants({align}), "mb-10", className)}
                {...props}
            >
                {/* Decorative ornament */}
                <div className="flex items-center justify-center gap-4 mb-4">
                    <span className="block h-px w-12 bg-primary/50"/>
                    <span className="text-primary text-lg font-['Cormorant_Garamond',serif]">✦</span>
                    <span className="block h-px w-12 bg-primary/50"/>
                </div>

                <h2 className="font-['Cormorant_Garamond',serif] text-2xl sm:text-3xl font-light tracking-[0.15em] uppercase text-foreground">
                    {title}
                </h2>
                {subtitle && (
                    <p className="mt-3 text-sm tracking-wider text-muted-foreground font-light">
                        {subtitle}
                    </p>
                )}
            </div>
        );
    }
);
SectionTitle.displayName = "SectionTitle";
