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
                <div className="flex flex-col md:flex-row md:items-start md:justify-between gap-10">
                    <div className="flex flex-col items-center md:items-start">
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
                        <div className="mt-4">
                            <FooterSocialLinks params={params}/>
                        </div>
                    </div>

                    {/* Legal Links */}
                    <div
                        className={cn("flex flex-col items-center md:items-start", isRtlLayout ? "md:text-right" : "md:text-left")}>
                        <h2 className="text-xs font-semibold tracking-[0.18em] uppercase text-foreground">
                            {t('LEGAL')}
                        </h2>
                        <ul className="mt-4 space-y-3 text-sm text-muted-foreground text-center md:text-start">
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
                </div>
                <div className="mt-10 border-t border-border pt-6 text-center">
                    <FooterRightReserved params={params}/>
                </div>
            </div>
        </footer>
    )
}

function FooterRightReserved({params}: { params: LayoutParams }) {
    const t = useTranslations("COMPONENTS.FOOTER");
    const year = new Date().getFullYear();

    return (
        <span className="text-xs text-muted-foreground">
            © {year}{" "}
            <Link href="/" className="font-semibold text-foreground hover:underline">
                {params.store.name}
            </Link>
            . {t("RIGHT_RESERVED")}
        </span>
    );
}

function FooterSocialLinks({params}: { params: LayoutParams }) {
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
        <div className="flex items-center gap-2">
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