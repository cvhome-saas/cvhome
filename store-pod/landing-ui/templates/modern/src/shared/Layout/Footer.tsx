'use client'
import * as React from 'react'
import {LayoutParams} from "@/types/params";
import {Link} from "@/i18n/navigation";
import Image from 'next/image';
import {useTranslations} from "next-intl";
import {Button} from "@/components/ui/button";
import {cn} from "@/lib/utils";
import {Facebook, Github, Instagram, Twitter} from "lucide-react";
import {isRtl} from "@/services/direction-utils";

export const Footer = ({params}: { params: LayoutParams }) => {
    const t = useTranslations('COMPONENTS.FOOTER');
    const isRtlLayout = isRtl(params.storeContext.locale);

    const legalLinks =
        params.contents?.content
            ?.filter(it => !it.linkToMenu)
            ?.filter(it => it.visible)
            ?.filter(it => it.description) ?? [];

    return (
        <footer className="mt-auto border-t border-border bg-background">
            <div className="mx-auto w-full max-w-7xl px-4 py-10 sm:px-6 lg:px-8">
                <div className="grid grid-cols-1 gap-10 md:grid-cols-12">
                    <div className="md:col-span-5">
                        <Link prefetch={false} href="/" className="group inline-flex items-center gap-3">
                            {params.store.logo && (
                                <Image
                                    src={params.store.logo.path}
                                    width={40}
                                    height={40}
                                    className="h-10 w-10 rounded-xl object-contain transition-transform duration-300 group-hover:scale-[1.03]"
                                    alt={params.store.logo.name}
                                />
                            )}
                            <span className="text-sm font-semibold tracking-[0.18em] uppercase text-foreground">
                                {params.store.name}
                            </span>
                        </Link>
                        <div className="mt-6">
                            <FooterSocialLinks params={params}/>
                        </div>
                    </div>

                    <div className="md:col-span-7">
                        <div className="grid grid-cols-2 gap-8 sm:grid-cols-3">
                            <div className={cn(isRtlLayout ? "text-end" : "text-start")}>
                                <h2 className="text-xs font-semibold tracking-[0.18em] uppercase text-foreground">
                                    {t('LEGAL')}
                                </h2>
                                <ul className="mt-4 space-y-3 text-sm text-muted-foreground">
                                    {legalLinks.map(it => (
                                        <li key={it.id}>
                                            <Link
                                                prefetch={false}
                                                href={`/content/${it.description!.friendlyUrl}`}
                                                className="hover:text-foreground transition-colors"
                                            >
                                                {it.description!.name}
                                            </Link>
                                        </li>
                                    ))}
                                </ul>
                            </div>

                            <div className="hidden sm:block"/>
                        </div>
                    </div>
                </div>

                <div className="mt-10 flex flex-col gap-4 border-t border-border pt-6 sm:flex-row sm:items-center sm:justify-between">
                    <FooterRightReserved params={params}/>
                </div>
            </div>
        </footer>
    )
}

interface FooterRightReservedProps extends React.HTMLAttributes<HTMLSpanElement> {
    params: LayoutParams;
}

function FooterRightReserved({params, className, ...props}: FooterRightReservedProps) {
    const t = useTranslations("COMPONENTS.FOOTER");
    const year = new Date().getFullYear();

    return (
        <span className={cn("text-xs text-muted-foreground", className)} {...props}>
            © {year}{" "}
            <Link href="/" className="font-semibold text-foreground hover:underline">
                {params.store.name}
            </Link>
            . {t("RIGHT_RESERVED")}
        </span>
    );
}

interface FooterSocialLinksProps extends React.HTMLAttributes<HTMLDivElement> {
    params: LayoutParams;
}

function FooterSocialLinks({params, className, ...props}: FooterSocialLinksProps) {
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
        <div className={cn("flex items-center gap-2", className)} {...props}>
            {params.store.socialLinks.map((link) => {
                const url = link.url.startsWith("http") ? link.url : `https://${link.url}`;
                const IconComponent = iconMap[link.provider.toLowerCase()];
                if (!IconComponent) return null;

                return (
                    <Button
                        key={link.provider}
                        variant="outline"
                        size="icon"
                        className="rounded-full"
                        asChild
                    >
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