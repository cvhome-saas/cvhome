import React from "react";

export const SectionTitle = ({
                                 titleText,
                                 positionClass,
                                 spaceClass,
                                 colorClass,
                             }) => {
    return (
        <div
            className={`section-title-5 ${positionClass ? positionClass : ""} ${spaceClass ? spaceClass : ""}`}>
            <h2 className={colorClass ? colorClass : ""}>{titleText}</h2>
        </div>
    );
};

