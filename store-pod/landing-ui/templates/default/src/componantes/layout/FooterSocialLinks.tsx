import {LayoutParams} from "@/types/params";
import {Button} from "@/components/ui/button";
import {cn} from "@/lib/utils";
import * as React from "react";
import {Facebook, Github, Instagram, Twitter} from "lucide-react";

export interface FooterSocialLinksProps extends React.HTMLAttributes<HTMLDivElement> {
    params: LayoutParams;
}

export async function FooterSocialLinks({params, className, ...props}: FooterSocialLinksProps) {
    const iconMap: Record<string, React.ElementType> = {
        facebook: Facebook,
        twitter: Twitter,
        x: Twitter,
        instagram: Instagram,
        github: Github
    };

    if (!params.store.socialLinks || params.store.socialLinks.length === 0) {
        return null;
    }

    return (
        <div className={cn("flex items-center space-x-4 mt-4 sm:justify-center sm:mt-0", className)} {...props}>
            {params.store.socialLinks.map((link) => {
                const url = link.url.startsWith("http") ? link.url : `https://${link.url}`;
                const IconComponent = iconMap[link.provider.toLowerCase()];

                if (!IconComponent) return null;

                return (
                    <Button key={link.provider} variant="ghost" size="icon" asChild>
                        <a href={url} target="_blank" rel="noopener noreferrer">
                            <IconComponent className="h-4 w-4"/>
                            <span className="sr-only">{link.provider}</span>
                        </a>
                    </Button>
                );
            })}
        </div>
    );
}
