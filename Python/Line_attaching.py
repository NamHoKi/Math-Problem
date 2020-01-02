# 블러링 했을 때 라인 수 != 안했을 때의 라인 수  ==> 잡티가 있음 ==> 아직 코드 추가 안함
# 완벽하진 않아도 이쁘게 찍은 사진 // 대충찍은 사진 ==> 지금은 처리 불가능 // 각도가 바르지 않으면 회전으로 라인 찾기

import cv2
import numpy as np
from matplotlib import pyplot as plt

file_name = input('File name : ')

org_img = cv2.imread(file_name,cv2.IMREAD_GRAYSCALE)
cv2.imshow('Original',org_img)
cv2.waitKey(0)
cv2.destroyAllWindows()

ret,inv_img = cv2.threshold(org_img, 120, 255, cv2.THRESH_BINARY_INV)   #밝기 뒤집기
cv2.imshow('INV_IMG',inv_img)
cv2.waitKey(0)
cv2.destroyAllWindows()

w = inv_img.shape[1]
h = inv_img.shape[0]

# 비어 있지 않은 줄 index // line 에 넣기
line = []
for i in range(0,h):
    for j in range(0,w):
        if inv_img[i][j] != 0:
            line.append(i)
            break

# 줄의 시작 끝 index를 edge에 저장
edge = [line[0]]
for i in range(0,len(line)-1):
    if line[i]+1 != line[i+1]:
        edge.append(line[i])
        edge.append(line[i+1])
edge.append(line[len(line)-1])
line_count = len(edge) // 2

widest_line = 00
for i in range(0,line_count):
    line_wide = edge[2*i + 1] - edge[2*i]
    if line_wide >= widest_line:
        widest_line = line_wide

# 가장 넓은 줄 기준의 크기로 각 줄 저장
for i in range(0,line_count):
    start = edge[2*i]
    end = start + widest_line
    cv2.imwrite('Line'+ str(i+1) +'.png',inv_img[start:end,:])
    img1 = cv2.imread('Line' + str(i + 1) + '.png', cv2.IMREAD_GRAYSCALE)
    cv2.imshow('Line' + str(i + 1), img1)
    cv2.waitKey(0)
    cv2.destroyAllWindows()

# 각 라인별로 저장한 이미지 붙이기
for i in range(0,line_count - 1):
    if i == 0:
        add_img = cv2.imread('Line' + str(i+1) + '.png', cv2.IMREAD_GRAYSCALE)
    img2 = cv2.imread('Line'+ str(i+2) +'.png',cv2.IMREAD_GRAYSCALE)
    add_img = cv2.hconcat([add_img,img2])
cv2.imwrite('Result.png',add_img)

print('넓이 :',w,'\n높이 :',h)
print('줄 수 :',line_count)
print('Edge index:',edge)
print('Widest line :',widest_line)