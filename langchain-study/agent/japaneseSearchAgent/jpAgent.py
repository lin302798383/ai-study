import os
from typing import Annotated, Sequence, TypedDict
from dotenv import load_dotenv
from langchain_community.vectorstores import Chroma
from langchain_core.messages import BaseMessage, HumanMessage
from langchain_core.messages import ToolMessage
from langchain_core.messages import SystemMessage
from langchain_openai import ChatOpenAI
from langchain_core.tools import tool
from langgraph.graph.message import add_messages
from langgraph.graph import StateGraph, END, START
from langgraph.prebuilt import ToolNode
from langchain_openai import OpenAIEmbeddings
from langchain_community.document_loaders import PyPDFLoader
from langchain.text_splitter import RecursiveCharacterTextSplitter

embeddings = OpenAIEmbeddings(model="BAAI/bge-m3", openai_api_base="https://api.siliconflow.cn/v1",
                              openai_api_key="sk-iouvjpxeohtxiutubutcojkbmehexxuqxtcbsgtmrtkidhuv")
pdf_path = "大日.pdf"

try:
    pdf_loader = PyPDFLoader(pdf_path)  # 先创建实例
    pages = pdf_loader.load()  # 再调用load方法
except Exception as e:
    print(f"加载pdf异常:{e}")
    raise

text_splitter = RecursiveCharacterTextSplitter(chunk_size=1000, chunk_overlap=200)
pages_split = text_splitter.split_documents(pages)

persist_dic = "./embedding"
collection_name = "da_ri"

if not os.path.exists(persist_dic):
    os.makedirs(persist_dic)

try:
    vectorStore = Chroma.from_documents(
        documents=pages_split,
        embedding=embeddings,
        persist_directory=persist_dic,
        collection_name=collection_name
    )

    print("创建向量数据库完成")
except Exception as e:
    print(f"创建向量数据库异常:{e}")
    raise

retriever = vectorStore.as_retriever(search_type="similarity", search_kwargs={"k": 5})


@tool()
def rag(query: str) -> str:
    """查询日语单词，语法和会话等信息"""
    docs = retriever.invoke(query)
    if not docs:
        return "没找到对应的内容"

    results = []
    for i, doc in enumerate(docs):
        results.append(f"搜索出的文档内容:doc{i + 1}:\n{doc.page_content}")
    return "\n\n".join(results)


class AgentState(TypedDict):
    messages: Annotated[Sequence[BaseMessage], add_messages]


tools = [rag]
model = ChatOpenAI(model="Qwen/Qwen3-8B", base_url="https://api.siliconflow.cn",
                   api_key="sk-iouvjpxeohtxiutubutcojkbmehexxuqxtcbsgtmrtkidhuv").bind_tools(tools)


def model_call(state: AgentState) -> AgentState:
    systemMsg = SystemMessage(
        "你是日语学习助手，如果用户想要查找日语单词或者语法等相关信息，你需要使用工具进行查询，并且返回对应的内容给用户，如果没匹配到对应的内容，则你需要自己去判别用户意图并进行回复。")
    response = model.invoke([systemMsg] + list(state['messages']))
    return {'messages': [response]}


def should_continue(state: AgentState) -> bool:
    messages = state['messages']
    last_message = messages[-1]
    # 结束条件是ai返回的消息中没有要求调用工具
    if not last_message.tool_calls:
        return False
    else:
        return True


tools_dict = {our_tool.name: our_tool for our_tool in tools}


def take_action(state: AgentState):
    tool_calls = state['messages'][-1].tool_calls
    results = []
    if not tool_calls:  # Add this check to handle cases with no tool calls
        return {'messages': [HumanMessage(content="No tools were requested.")]}

    for t in tool_calls:
        print(f"Calling Tool: {t['name']} with query: {t['args'].get('query', 'No query provided')}")
        if t['name'] not in tools_dict:  # Fixed indentation and condition
            print(f"Tool: {t['name']} does not exist.")
            result = "Incorrect Tool Name, Please Retry with a valid tool."
        else:
            result = tools_dict[t['name']].invoke(t['args'])
            print(f"Result length: {len(str(result))}")
        results.append(ToolMessage(tool_call_id=t['id'], name=t['name'], content=str(result)))
    print("Tools Execution Complete. Back to the model!")
    return {'messages': results}


graph = StateGraph(AgentState)
graph.add_node("llm", model_call)
graph.add_node("rag", take_action)
graph.add_conditional_edges(
    "llm",
    should_continue,
    {
        True: "rag", False: END
    }
)
graph.add_edge("rag", "llm")
graph.set_entry_point("llm")

app = graph.compile()
# app.get_graph().draw_mermaid_png(output_file_path="jpAgent.png")

def print_stream(stream):
    for s in stream:
        message = s["messages"][-1]
        if (isinstance(message, tuple)):
            print(message)
        else:
            message.pretty_print()

def running_agent():
    print("agent启动")
    while True:
        user_cmd = input("输入你的问题\n")
        if user_cmd == "end":
            break
        messages = [HumanMessage(user_cmd)]
        result = app.stream({"messages": messages}, stream_mode="values")
        print_stream(result)



running_agent()