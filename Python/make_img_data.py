#python 3.7 - 'UTF-8'

import numpy as np
import cv2 as cv

global check,start_point,end_point,key
check = 0
start_point = [] #왼쪽 위 끝점
end_point = [] #오른쪽 아래 끝점
key = []
img_count = 0
img_size = 20 #10*10

mouse_event_types = { 0:"cv.EVENT_MOUSEMOVE", 1:"cv.EVENT_LBUTTONDOWN", 2:"cv.EVENT_RBUTTONDOWN", 3:"cv.EVENT_MBUTTONDOWN",
                 4:"cv.EVENT_LBUTTONUP", 5:"cv.EVENT_RBUTTONUP", 6:"cv.EVENT_MBUTTONUP",
                 7:"cv.EVENT_LBUTTONDBLCLK", 8:"cv.EVENT_RBUTTONDBLCLK", 9:"cv.EVENT_MBUTTONDBLCLK",
                 10:"cv.EVENT_MOUSEWHEEL", 11:"cv.EVENT_MOUSEHWHEEL"}

mouse_event_flags = { 0:"None", 1:"cv.EVENT_FLAG_LBUTTON", 2:"cv.EVENT_FLAG_RBUTTON", 4:"cv.EVENT_FLAG_MBUTTON",

                8:"cv.EVENT_FLAG_CTRLKEY", 9:"cv.EVENT_FLAG_CTRLKEY + cv.EVENT_FLAG_LBUTTON",
                10:"cv.EVENT_FLAG_CTRLKEY + cv.EVENT_FLAG_RBUTTON", 11:"cv.EVENT_FLAG_CTRLKEY + cv.EVENT_FLAG_MBUTTON",

                16:"cv.EVENT_FLAG_SHIFTKEY", 17:"cv.EVENT_FLAG_SHIFTKEY + cv.EVENT_FLAG_LBUTTON",
                18:"cv.EVENT_FLAG_SHIFTLKEY + cv.EVENT_FLAG_RBUTTON", 19:"cv.EVENT_FLAG_SHIFTKEY + cv.EVENT_FLAG_MBUTTON",

                32:"cv.EVENT_FLAG_ALTKEY", 33:"cv.EVENT_FLAG_ALTKEY + cv.EVENT_FLAG_LBUTTON",
                34:"cv.EVENT_FLAG_ALTKEY + cv.EVENT_FLAG_RBUTTON", 35:"cv.EVENT_FLAG_ALTKEY + cv.EVENT_FLAG_MBUTTON"}

def mouse_callback(event, x, y, flags, param):
    global check, start_point, end_point, key
    if mouse_event_types[event] == 'cv.EVENT_LBUTTONDOWN':
        print( '( '+ str(x) + ',' + str(y), ')')
        if check == 0:
            start_point.append([x,y])
            check += 1
        elif check == 1:
            end_point.append([x,y])
            check += 1
            temp = input('Key : ') #  0: 빈칸  //  1: 텍스트  //  2: 그림
            key.append(temp)
            check = 0

img_file_name = input('Image File Name : ')
img1 = cv.imread(img_file_name,cv.IMREAD_GRAYSCALE)
img1_array = np.array(img1)
x_max = len(img1_array[0]) - 1
y_max = len(img1_array) - 1

cv.namedWindow('Test')
cv.setMouseCallback('Test', mouse_callback)

while(1):
    cv.imshow('Test',img1)
    k = cv.waitKey(1) & 0xFF
    if k == 27: # key(27) == ESCAPE
        cv.destroyAllWindows()
        break

print(start_point,end_point,key)
for i in range(0,y_max-img_size):
    for j in range(0,x_max-img_size):
        for l in range(0,len(key)):
            if i >= start_point[l][1] and j >= start_point[l][0] and i + img_size <= end_point[l][1] and j + img_size <= end_point[l][0]:
                cv.imwrite(key[l] + ','+ str(img_count) + '.png',img1_array[i:i+img_size,j:j+img_size])
                img_count += 1
                break
            if l == len(key)-1:
                cv.imwrite('0,' + str(img_count) + '.png',img1_array[i:i+img_size,j:j+img_size])
                img_count += 1