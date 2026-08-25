'use client'
import {useState} from 'react';
import Image from 'next/image';
import type {ImageFile} from '@store-front/types';

/** The merchant's logo on the wordmark strip; when there is none, or it fails to load, the name in poster caps. */
export function Wordmark({logo, name}: { logo: ImageFile | undefined; name: string }) {
    const [failed, setFailed] = useState(false);
    if (logo?.path && !failed) {
        return <Image src={logo.path} alt={logo.name || name} width={120} height={32} priority onError={() => setFailed(true)} className="h-6 w-auto object-contain lg:h-7"/>;
    }
    return <bdi className="block min-w-0 truncate">{name}</bdi>;
}
