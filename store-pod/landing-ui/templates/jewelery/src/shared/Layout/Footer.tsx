'use client'
import * as React from 'react'
import {LayoutParams} from "@/types/params";
import {Link} from "@/i18n/navigation";
import Image from 'next/image';
import {useTranslations} from "next-intl";
import {cn} from "@/lib/utils";
import {Facebook, Github, Instagram, Twitter} from "lucide-react";
import {isRtl} from "@/services/direction-utils";

export const Footer = ({params}: { params: LayoutParams }) => {
    const t = useTranslations('COMPONENTS.FOOTER');
    const isRtlLayout = isRtl(params.storeContext.locale);

    return (
        <footer className="bg-foreground text-background mt-auto">
            {/* Decorative top divider */}
            <div className="h-px bg-primary/40"/>

            <div className="mx-auto w-full max-w-screen-xl px-6 py-12 lg:py-16">
                {/* Brand + links row */}
                <div className="grid grid-cols-1 md:grid-cols-3 gap-10 mb-10">
                    {/* Brand column */}
                    <div className={`${isRtlLayout ? 'text-end' : 'text-start'}`}>
                        <Link prefetch={false} href="/" className="inline-flex items-center gap-3 mb-4">
                            {params.store.logo ? (
                                <Image
                                    src={params.store.logo.path}
                                    width={36}
                                    height={36}
                                    className="h-9 w-auto"
                                    alt={params.store.logo.name}
                                />
                            ) : null}
                            <span
                                className="font-['Cormorant_Garamond',serif] text-xl font-light tracking-[0.2em] uppercase text-primary">
                                {params.store.name}
                            </span>
                        </Link>
                        <p className="text-xs tracking-wider text-background/60 leading-relaxed max-w-xs">
                            {/* Tagline placeholder — store config may override */}
                        </p>
                    </div>

                    {/* Empty middle column for spacing */}
                    <div/>

                    {/* Legal links column */}
                    {params.contents && params.contents.content && params.contents.content.length > 0 && (
                        <div className={`${isRtlLayout ? 'text-end' : 'text-start'}`}>
                            <h3 className="text-[10px] tracking-[0.3em] uppercase font-medium text-primary mb-5">
                                {t('LEGAL')}
                            </h3>
                            <ul className="space-y-3">
                                {params.contents.content
                                    .filter(it => !it.linkToMenu)
                                    .filter(it => it.visible)
                                    .filter(it => it.description)
                                    .map(it => (
                                        <li key={it.id}>
                                            <Link
                                                prefetch={false}
                                                href={`/content/${it.description.friendlyUrl}`}
                                                className="text-xs tracking-wider text-background/70 hover:text-primary transition-colors duration-200">
                                                {it.description.name}
                                            </Link>
                                        </li>
                                    ))}
                            </ul>
                        </div>
                    )}
                </div>

                {/* Thin divider */}
                <div className="h-px bg-background/10 mb-8"/>

                {/* Bottom row */}
                <div className="flex flex-col sm:flex-row items-center justify-between gap-4">
                    <FooterRightReserved params={params}/>
                    <FooterSocialLinks params={params}/>
                </div>
            </div>
        </footer>
    );
};

interface FooterRightReservedProps extends React.HTMLAttributes<HTMLSpanElement> {
    params: LayoutParams;
}

function FooterRightReserved({params, className, ...props}: FooterRightReservedProps) {
    const t = useTranslations("COMPONENTS.FOOTER");
    const year = new Date().getFullYear();

    return (
        <span className={cn("text-[10px] tracking-widest uppercase text-background/50", className)} {...props}>
            © {year}{' '}
            <Link href="/" className="hover:text-primary transition-colors font-medium text-background/70">
                {params.store.name}
            </Link>
            {'. '}{t("RIGHT_RESERVED")}
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
        github: Github,
    };

    if (!params.store.socialLinks || params.store.socialLinks.length === 0) {
        return null;
    }

    return (
        <div className={cn("flex items-center gap-4", className)} {...props}>
            {params.store.socialLinks.map((link) => {
                const url = link.url.startsWith("http") ? link.url : `https://${link.url}`;
                const IconComponent = iconMap[link.provider.toLowerCase()];
                if (!IconComponent) return null;

                return (
                    <a
                        key={link.provider}
                        href={url}
                        target="_blank"
                        rel="noopener noreferrer"
                        className="text-background/50 hover:text-primary transition-colors duration-200"
                        aria-label={link.provider}
                    >
                        <IconComponent className="h-4 w-4"/>
                    </a>
                );
            })}
        </div>
    );
}
