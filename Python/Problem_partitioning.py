import cv2
import numpy as np
from matplotlib import pyplot as plt

blank_max = 20

file_name = input('File name : ')
# 기준 선 찾기
org_img = cv2.imread(file_name) #불러올 파일
img = cv2.imread(file_name,0) #불러올 파일
ret,img = cv2.threshold(img, 240, 255, cv2.THRESH_BINARY_INV) #밝기 뒤집기
img1_arry = np.array(img)
org_img_arry = np.array(org_img)

max = 0
w = img.shape[1]
h = img.shape[0]

for x in range(0,w):
    sum = 0
    for y in range(0,h):
        sum = sum + img1_arry[y,x]
    if sum > max:
        max = sum
        ct = x
print("center line : ",ct)

# 기준점 찾기
blank_count = 0  #빈줄 개수
img_cnt = 1
start_index = 65 #시작줄
check_index = 65 #체크줄
check = 65
min_size = 30 #문제가 되기위한 최소 크기

for i in range(65,h-1):
    sum1 = 0
    sum2 = 0
    for j in range(0,ct-1):
        sum1 = sum1 + img[i,j]
        sum2 = sum2 + img[i+1,j]
    if sum1 == 0:
        blank_count = blank_count + 1
    else:
        blank_count = 0

    if (sum1 == 0 and sum2 != 0) and (start_index == 65 or blank_count >= blank_max):
        start_index = i
        blank_count = 0
        check = 1
    elif sum1 != 0 and sum2 == 0:
        check_index = i

    if check == 1 and blank_count > blank_max:
        if check_index - start_index >= min_size:
            cv2.imwrite("Img" + str(img_cnt) + ".png", org_img_arry[start_index: check_index, 0: ct - 1]) #저장 이름
            img_cnt = img_cnt + 1
            print('Save image : ', img_cnt-1)
            check = 0

blank_count = 0
start_index = 65
check_index = 65
check = 65

for i in range(65,h-1):
    sum1 = 0
    sum2 = 0
    for j in range(ct+3,w):
        sum1 = sum1 + img[i,j]
        sum2 = sum2 + img[i+1,j]
    if sum1 == 0:
        blank_count = blank_count + 1
    else:
        blank_count = 0

    if (sum1 == 0 and sum2 != 0) and (start_index == 65 or blank_count >= blank_max):
        start_index = i
        blank_count = 0
        check = 1
    elif sum1 != 0 and sum2 == 0:
        check_index = i

    if check == 1 and blank_count > blank_max:
        if check_index - start_index >= min_size:
            cv2.imwrite("Img" + str(img_cnt) + ".png", org_img_arry[start_index: check_index, ct+3 : w])
            img_cnt = img_cnt + 1
            print('Save image : ', img_cnt-1)
            check = 0