class RecordDataObject(object):
    def __init__(self, date=None, time=None, sleep=0, light=0, motion=0, realSleep=0):
        self.date = date
        self.time = time
        self.sleep = int(sleep)#-1
        self.light = int(light)#-1
        self.motion = motion
        self.realSleep = realSleep

        