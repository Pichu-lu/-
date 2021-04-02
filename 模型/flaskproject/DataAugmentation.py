
import cv2
import numpy as np


def crop(image):
    image_shape = image.shape
    height = image_shape[0]
    width = image_shape[1]
    save_size = width if width < height else height
    height_crop = (height - save_size) // 2
    width_crop = (width - save_size) // 2
    if height_crop > 0:
        image = image[height_crop:-height_crop, :, :]
    if width_crop > 0:
        image = image[:, width_crop:-width_crop, :]
    if image.shape[0] > 1000:
        crop_size = (image.shape[0] * 2) // 3
        height_crop = (image.shape[1] - crop_size) // 2
        width_crop = (image.shape[0] - crop_size) // 2
        if height_crop > 0:
            image = image[height_crop:-height_crop, :, :]
        if width_crop > 0:
            image = image[:, width_crop:-width_crop, :]
    return image


def contrast_enhance(image):
    a = 1.9
    image = image * float(a)
    image[image > 255] = 255
    image = np.round(image)
    image = image.astype(np.uint8)
    return image


def sharpe(image, number):
    for i in range(number):
        image = cv2.medianBlur(image, 9)
        image_out = np.zeros(image.shape, np.uint8)
        cv2.Laplacian(image, -1, image_out, 5)
        image = cv2.subtract(image, image_out)
        image = cv2.medianBlur(image, 9)
    return image


def sobel_sharpening(image):
    gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)

    gradX = cv2.Sobel(gray, ddepth=cv2.cv2.CV_32F, dx=1, dy=0, ksize=-1)
    gradY = cv2.Sobel(gray, ddepth=cv2.cv2.CV_32F, dx=0, dy=1, ksize=-1)

    gradient = cv2.subtract(gradX, gradY)
    gradient = cv2.convertScaleAbs(gradient)

    blurred = cv2.blur(gradient, (13, 13))
    (_, thresh) = cv2.threshold(blurred, 90, 255, cv2.THRESH_BINARY)

    kernel = cv2.getStructuringElement(cv2.MORPH_ELLIPSE, (50, 50))
    closed = cv2.morphologyEx(thresh, cv2.MORPH_CLOSE, kernel)

    erode = cv2.erode(closed, None, iterations=5)
    dilate = cv2.dilate(erode, None, iterations=5)

    (cnts, _) = cv2.findContours(closed, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)
    c = sorted(cnts, key=cv2.contourArea, reverse=True)[0]

    rect = cv2.minAreaRect(c)
    box = np.int0(cv2.boxPoints(rect))

    Xs = [i[0] for i in box]
    Ys = [i[1] for i in box]
    x1 = min(Xs) if min(Xs) > 0 else 0
    x2 = max(Xs) if max(Xs) < image.shape[0] else image.shape[0]
    y1 = min(Ys) if min(Ys) > 0 else 0
    y2 = max(Ys) if max(Ys) < image.shape[1] else image.shape[1]

    height = y2 - y1
    width = x2 - x1

    return y1 + 10, height + 10, x1 + 10, width + 10


def pad255(image):
    width, height, _ = image.shape
    top_size, bottom_size, left_size, right_size = 0, 0, 0, 0
    if width > height:
        size_all = width - height
        left_size, right_size = size_all // 2, size_all // 2
        if left_size + right_size != size_all:
            left_size += 1
    else:
        size_all = height - width
        top_size, bottom_size = size_all // 2, size_all // 2
        if top_size + bottom_size != size_all:
            top_size += 1
    constant = cv2.copyMakeBorder(image, top_size, bottom_size, left_size,
                                  right_size, cv2.BORDER_CONSTANT,
                                  value=(255, 255, 255))
    return constant


def object_detection(image_file):
    srcimage = cv2.imread(image_file)
    cropimage = crop(srcimage)
    sharpeimage = sharpe(cropimage.copy(), 1)
    enhaneceimage = contrast_enhance(sharpeimage.copy())
    y1, height, x1, width = sobel_sharpening(enhaneceimage.copy())
    boximage = cropimage[y1:y1 + height, x1:x1 + width]
    dstimage = pad255(boximage)
    return dstimage



