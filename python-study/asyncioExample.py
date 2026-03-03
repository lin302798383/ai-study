import asyncio


async def main():
    t1 = asyncio.create_task(otherWork1())
    t2 = asyncio.create_task(otherWork2())
    await t1
    await t2


async def otherWork1():
    print("work1 begin")
    await asyncio.sleep(1)
    print("work1 end")


async def otherWork2():
    print("work2 begin")
    await asyncio.sleep(1)
    print("work2 end")


asyncio.run(main())
