'use client'
import Image, {ImageProps} from 'next/image';
import {useState} from 'react';

interface FallbackImageProps extends ImageProps {
    fallbackSrc?: string; // Optional fallback image source
}

const FallbackImage: React.FC<FallbackImageProps> = ({fallbackSrc, sizes, width, height, src, ...props}) => {
    const [imgSrc, setImgSrc] = useState(src);
    const [errorOccurred, setErrorOccurred] = useState(false); // Track if fallback has already been applied

    const handleError = () => {
        // Apply fallback only if it hasn't been set yet
        if (!errorOccurred && fallbackSrc) {
            setImgSrc(fallbackSrc);
            setErrorOccurred(true); // Prevent further errors from looping
        }
    };

    return (
        <Image
            {...props}
            src={imgSrc}
            sizes={sizes}
            width={width}
            height={height}
            onError={handleError}
        />
    );
};

export default FallbackImage;