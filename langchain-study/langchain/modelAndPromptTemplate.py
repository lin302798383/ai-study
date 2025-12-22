from dotenv import load_dotenv

load_dotenv()

from langchain_openai import ChatOpenAI

model = ChatOpenAI(model="Qwen/Qwen3-8B", base_url="https://api.siliconflow.cn")

from langchain_core.messages import HumanMessage, SystemMessage

# messages = [
#     SystemMessage(content="Translate the following from English into chinese"),
#     HumanMessage(content="hi!"),
# ]
#
# result = model.invoke(messages)
# print(result)

from langchain_core.prompts import ChatPromptTemplate

promptTemplate = ChatPromptTemplate.from_messages(
    [("system", "你是一个{language}学习助手"), ("user", "请告诉我{word}的意思和用法")])

# prompt = promptTemplate.invoke({"language": "日语", "word": "騒めく"})
# print(prompt)
# print(prompt.to_messages())
# for token in model.stream(prompt):
#     print(token.content, end="")

response = model.batch(
    [
        "你好",
        "云顶之奕是什么",
        "英雄联盟是什么"
    ]
)

print(response)



