import os
from typing import Annotated, Sequence, TypedDict
from dotenv import load_dotenv
from langchain_core.messages import BaseMessage, HumanMessage
from langchain_core.messages import ToolMessage
from langchain_core.messages import SystemMessage
from langchain_openai import ChatOpenAI
from langchain_core.tools import tool
from langgraph.graph.message import add_messages
from langgraph.graph import StateGraph, END, START
from langgraph.prebuilt import ToolNode

# 获取当前文件的目录
current_dir = os.path.dirname(os.path.abspath(__file__))
# 获取上一级目录
parent_dir = os.path.dirname(current_dir)
# 构建上一级目录中的.env文件路径
env_path = os.path.join(parent_dir, '.env')

# 加载.env文件
load_dotenv(env_path)


class AgentState(TypedDict):
    messages: Annotated[Sequence[BaseMessage], add_messages]


@tool
def addTool(a: int, b: int):
    """返回两数相加的和"""
    return a + b


@tool
def multTool(a: int, b: int):
    """返回两数相乘的积"""
    return a * b


tools = [addTool, multTool]

model = ChatOpenAI(model="Qwen/Qwen3-8B", base_url="https://api.siliconflow.cn").bind_tools(tools)


def model_call(state: AgentState) -> AgentState:
    response = model.invoke(state['messages'])
    # 使用了annotated,add_messages会将消息添加到序列的最后
    return {'messages': [response]}


def should_continue(state: AgentState) -> AgentState:
    messages = state['messages']
    last_message = messages[-1]
    # 结束条件是ai返回的消息中没有要求调用工具
    if not last_message.tool_calls:
        return "end"
    else:
        return "toolCall"


graph = StateGraph(AgentState)

graph.add_node("processor", model_call)
# toolNode默认会取状态中的messages作为参数
tool_node = ToolNode(tools)
graph.add_node("tools", tool_node)

graph.add_edge(START, "processor")
graph.add_edge("tools", "processor")
graph.add_conditional_edges("processor",
                            should_continue,
                            {
                                "end": END,
                                "toolCall": "tools"}
                            )

app = graph.compile()


# app.get_graph().draw_mermaid_png(output_file_path="chatRobot.png")


def print_stream(stream):
    for s in stream:
        message = s["messages"][-1]
        if (isinstance(message, tuple)):
            print(message)
        else:
            message.pretty_print()


input = HumanMessage("(1+5)*6=?，并且告诉我这个数是否大于100")
result = app.stream({"messages": [input]}, stream_mode="values")
print_stream(result)
