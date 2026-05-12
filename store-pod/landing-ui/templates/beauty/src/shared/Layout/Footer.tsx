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

    return (
        <footer className="bg-secondary/20 border-t border-primary/10 mt-auto pt-12 pb-8">
            <div className="mx-auto w-full max-w-screen-xl px-4 py-6 lg:py-8">
                <div className="md:flex md:justify-between items-start">
                    <div className="mb-8 md:mb-0">
                        <Link prefetch={false} href={"/"} className="flex items-center gap-2 group">
                            {
                                params.store.logo &&
                                <Image src={params.store.logo.path}
                                       width={40}
                                       height={40}
                                       className={`h-10 w-auto transition-transform duration-500 group-hover:scale-110 ${isRtlLayout ? 'ms-3' : 'me-3'}`}
                                       alt={params.store.logo.name}/>
                            }
                            <span
                                className="self-center text-3xl font-serif font-bold tracking-tight text-primary">
                                {params.store.name}
                            </span>
                        </Link>
                        <p className="mt-4 max-w-xs text-muted-foreground text-sm font-light leading-relaxed">
                            Elevating your beauty journey with curated selections of premium products. Elegance is not
                            just about being noticed, but being remembered.
                        </p>
                    </div>
                    {
                        params.contents && params.contents.content && params.contents.content.length > 0 &&
                        <div className={`grid grid-cols-2 gap-8 sm:gap-6 sm:grid-cols-3`}>
                            <div></div>
                            <div></div>
                            <div className={`${isRtlLayout ? 'text-end' : 'text-start'}`}>
                                <h2 className="mb-6 text-sm font-semibold text-neutral uppercase dark:text-foreground">
                                    {t('LEGAL')}
                                </h2>
                                <ul className="text-neutral dark:text-neutral font-medium">
                                    {
                                        params.contents.content
                                            .filter(it => !it.linkToMenu)
                                            .filter(it => it.visible)
                                            .filter(it => it.description)
                                            .map(it => {
                                                return <li key={it.id} className="mb-4">
                                                    <Link prefetch={false}
                                                          href={`/content/${it.description.friendlyUrl}`}
                                                          className="hover:underline">
                                                        {it.description.name}
                                                    </Link>
                                                </li>

                                            })
                                    }
                                </ul>
                            </div>
                        </div>
                    }
                </div>
                <hr className="my-6 border-neutral sm:mx-auto dark:border-neutral lg:my-8"/>
                <div className="sm:flex sm:items-center sm:justify-between">
                    <FooterRightReserved params={params}/>
                    <FooterSocialLinks params={params}/>
                </div>
            </div>
        </footer>
    )
}


interface FooterRightReservedProps extends React.HTMLAttributes<HTMLSpanElement> {
    params: LayoutParams;
}

function FooterRightReserved({
                                 params,
                                 className,
                                 ...props
                             }: FooterRightReservedProps) {
    const t = useTranslations("COMPONENTS.FOOTER");
    const year = new Date().getFullYear();

    return (
        <span className={cn("text-sm text-muted-foreground sm:text-center", className)} {...props}>
      © {year} <Link href="/"
                     className="hover:underline font-semibold text-foreground">{params.store.name}</Link>. {t("RIGHT_RESERVED")}
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
