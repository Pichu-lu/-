import glob
import os

import torch
from numpy import *
from PIL import Image, ImageDraw
from torch import nn
from torchvision import datasets, models
from torchvision.transforms import transforms

from DataAugmentation import *


class MyTransform(object):
    def __call__(self, img):
        srcimage = cv2.cvtColor(asarray(img), cv2.COLOR_RGB2BGR)
        cropimage = crop(srcimage)
        sharpeimage = sharpe(cropimage.copy(), 1)
        enhaneceimage = contrast_enhance(sharpeimage.copy())
        y1, height, x1, width = sobel_sharpening(enhaneceimage.copy())
        boximage = cropimage[y1:y1 + height, x1:x1 + width]
        padimage = pad255(boximage)
        dstimage = Image.fromarray(cv2.cvtColor(padimage, cv2.COLOR_BGR2RGB))
        return dstimage


image_transforms = {
    'test': transforms.Compose([
        transforms.CenterCrop((600, 600)),
        # MyTransform(),
        transforms.Resize((224, 224)),
        transforms.ToTensor(),
        transforms.Normalize([0.485, 0.456, 0.406], [0.229, 0.224, 0.225])
    ])
}
data = {
    'test': datasets.ImageFolder(root="./static/datasets/test_set", transform=image_transforms['test'])
}

idx_to_class = {0: "lowerDosidicus", 1: "lowerIllex", 2: "lowerSthenoeuthis",
                3: "upperDosidicus", 4: "upperIllex", 5: "upperSthenoeuthis"}


def model_resnet50():
    model = models.resnet50(pretrained=False)
    num_fcs = model.fc.in_features
    model.fc = nn.Sequential(
        nn.Linear(num_fcs, 256),
        nn.Dropout(0.2),
        nn.ReLU(inplace=True),
        nn.Linear(256, 6),
    )
    return model


def predict(test_image_name):
    results = {}
    model = model_resnet50()

    # model = models.resnet50(pretrained=True)
    # num_ftrs = model.fc.in_features
    # model.fc = nn.Linear(num_ftrs, 6)

    checkpoint = torch.load('./static/checkpoint/resnet50_20/best_model.pt')
    model.load_state_dict(checkpoint['net'])

    device = torch.device("cuda:0" if torch.cuda.is_available() else "cpu")

    model = model.to(device)
    transform = image_transforms['test']

    test_image = Image.open(test_image_name)
    draw = ImageDraw.Draw(test_image)

    test_image_tensor = transform(test_image)

    if torch.cuda.is_available():
        test_image_tensor = test_image_tensor.view(1, 3, 224, 224).cuda()
    else:
        test_image_tensor = test_image_tensor.view(1, 3, 224, 224)

    with torch.no_grad():
        model.eval()
        out = model(test_image_tensor)
        ps = torch.exp(out)
        topk, topclass = ps.topk(6, dim=1)
        for i in range(6):
            result = {"name": idx_to_class[topclass.cpu().numpy()[0][i]],
                      "score": np.asscalar(topk.cpu().numpy()[0][i])}
            temp = {"result" + str(i): result}
            results.update(temp)
    print(results)
    return results


if __name__ == '__main__':
    # image_dir = "./static/data/123/upperIllex/IMG_6621.JPG"
    image_dir = "./static/data/123"
    for label in os.listdir(image_dir):
        image_list = glob.glob(os.path.join(image_dir, label, "*.JPG"))
        for i in image_list:
            predict(i)
