import React from "react";

export const SectionTitle: React.FC<{ title: string }> = ({title}) => {
    return (
        <div className="text-center">
            <h2 className="text-2xl md:text-3xl font-bold text-foreground">
                {title}
            </h2>
        </div>
    );
};