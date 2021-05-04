import createSleepModel as csm




loss, accuracy = csm.start04(5,24)
loss1, accuracy1 = csm.start04(10,11)
loss2, accuracy2 = csm.start04(30,3)

loss3, accuracy3 = csm.start12(5,24)
loss4, accuracy4 = csm.start12(10,11)
loss5, accuracy5 = csm.start12(30,3)

print('1. ' + str(loss) +'. and .'+ str(accuracy))
print('2. ' + str(loss1) +'. and .'+ str(accuracy1))
print('3. ' + str(loss2) +'. and .'+ str(accuracy2))
print('4. ' + str(loss3) +'. and .'+ str(accuracy3))
print('5. ' + str(loss4) +'. and .'+ str(accuracy4))
print('6. ' + str(loss5) +'. and .'+ str(accuracy5))
